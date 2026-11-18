package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	public static Model read(String fileContent) {
		Model result = new Model();

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			++lineInd;
			int comment = line.indexOf('#');
			if (comment >= 0) line = line.substring(0, comment);
			line = line.trim();
			if (line.isEmpty()) continue;
			ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			switch (token) {
				case OBJ_VERTEX_TOKEN -> result.vertices.add(parseVertex(wordsInLine, lineInd));
				case OBJ_TEXTURE_TOKEN -> result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
				case OBJ_NORMAL_TOKEN -> result.normals.add(parseNormal(wordsInLine, lineInd));
				case OBJ_FACE_TOKEN -> {
					Polygon polygon = parseFace(wordsInLine, lineInd);
					resolveIndices(polygon.getVertexIndices(), result.vertices.size(), lineInd);
					resolveIndices(polygon.getTextureVertexIndices(), result.textureVertices.size(), lineInd);
					resolveIndices(polygon.getNormalIndices(), result.normals.size(), lineInd);
					result.polygons.add(polygon);
				}
				default -> {}
			}
		}

		if (result.vertices.isEmpty() || result.polygons.isEmpty()) {
			throw new ObjReaderException("Model must contain vertices and faces.", lineInd);
		}
		return result;
	}

	private static void resolveIndices(ArrayList<Integer> indices, int size, int lineInd) {
		for (int i = 0; i < indices.size(); i++) {
			int index = indices.get(i);
			// parseFaceWord converts OBJ indices to zero-based, including negative ones.
			if (index < -1) index = size + index + 1;
			if (index < 0 || index >= size) throw new ObjReaderException("Index out of range.", lineInd);
			indices.set(i, index);
		}
	}

	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					parseFinite(wordsInLineWithoutToken.get(0)),
					parseFinite(wordsInLineWithoutToken.get(1)),
					parseFinite(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few vertex arguments.", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector2f(
					parseFinite(wordsInLineWithoutToken.get(0)),
					parseFinite(wordsInLineWithoutToken.get(1)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			return new Vector3f(
					parseFinite(wordsInLineWithoutToken.get(0)),
					parseFinite(wordsInLineWithoutToken.get(1)),
					parseFinite(wordsInLineWithoutToken.get(2)));

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few normal arguments.", lineInd);
		}
	}

	private static float parseFinite(String value) {
		float number = Float.parseFloat(value);
		if (!Float.isFinite(number)) throw new NumberFormatException("Non-finite number");
		return number;
	}

	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.size() < 3) throw new ObjReaderException("Face must have at least three vertices.", lineInd);
		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<>();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices, onePolygonNormalIndices, lineInd);
		}
		int count = onePolygonVertexIndices.size();
		if ((!onePolygonTextureVertexIndices.isEmpty() && onePolygonTextureVertexIndices.size() != count)
				|| (!onePolygonNormalIndices.isEmpty() && onePolygonNormalIndices.size() != count)) {
			throw new ObjReaderException("Inconsistent face attributes.", lineInd);
		}

		Polygon result = new Polygon();
		result.setVertexIndices(onePolygonVertexIndices);
		result.setTextureVertexIndices(onePolygonTextureVertexIndices);
		result.setNormalIndices(onePolygonNormalIndices);
		return result;
	}

	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd) {
		try {
			String[] wordIndices = wordInLine.split("/", -1);
			switch (wordIndices.length) {
				case 1 -> {
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
				}
				case 2 -> {
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
					onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
				}
				case 3 -> {
					onePolygonVertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
					onePolygonNormalIndices.add(Integer.parseInt(wordIndices[2]) - 1);
					if (!wordIndices[1].equals("")) {
						onePolygonTextureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
					}
				}
				default -> {
					throw new ObjReaderException("Invalid element size.", lineInd);
				}
			}

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments.", lineInd);
		}
	}
}
