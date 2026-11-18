Unicode true
Name "Omni Viewer"
OutFile "${OUTPUT}"
Icon "${ICON}"
SilentInstall silent
RequestExecutionLevel user
SetCompressor /SOLID lzma

Section
  InitPluginsDir
  SetOutPath "$PLUGINSDIR"
  File /r "${APP_IMAGE}\*"
  ExecWait '"$PLUGINSDIR\OmniViewer.exe"'
SectionEnd
