#!/bin/sh
for dir in */; do
  pushd $dir > /dev/null

  ARTIFACT_URL=`cat url`
  CACHED_ARTIFACT_URL=`cat url-cached 2> /dev/null`

  if [ ! "$ARTIFACT_URL" == "$CACHED_ARTIFACT_URL" ]; then
    if [ ! -z "$CACHED_ARTIFACT_URL" ]; then
      echo Cache busted for "$ARTIFACT_URL", downloading
    else
      echo No cache for "$ARTIFACT_URL", downloading
    fi

    curl --output bin "$ARTIFACT_URL" && echo "$ARTIFACT_URL" > url-cached
  fi

  popd > /dev/null
done