#/bin/bash
#
# script to run imageviewer on Linux
#
# some advanced bash and unix tricks to find the imageviewer install directory
## Linux
LSOF=$(lsof -p $$ | grep -E "/"$(basename $0)"$")
IMAGEVIEWER_SCRIPT=$(echo $LSOF | sed -r s/'^([^\/]+)\/'/'\/'/1 2>/dev/null)
if [ $? -ne 0 ]; then
## OSX
  IMAGEVIEWER_SCRIPT=$(echo $LSOF | sed -E s/'^([^\/]+)\/'/'\/'/1 2>/dev/null)
fi
IMAGEVIEWER_HOME=$(dirname $IMAGEVIEWER_SCRIPT)
export PYTHONPATH=$IMAGEVIEWER_HOME/python/linux:$PYTHONPATH
export LD_PRELOAD=/usr/lib/libpython2.5.so.1.0
$IMAGEVIEWER_HOME/imageviewer

