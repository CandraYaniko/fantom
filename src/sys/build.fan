#! /usr/bin/env fansubstitute
//
// Copyright (c) 2006, Brian Frank and Andy Frank
// Licensed under the Academic Free License version 3.0
//
// History:
//   4 Nov 06  Brian Frank  Creation
//

using build

**
** Build: sys
**
class Build : BuildPod
{

  override Void setup()
  {
    podName     = "sys"
    version     = globalVersion
    description = "Fan system runtime"
    depends     = Str[,]
    srcDirs     = [`fan/`]
    resDirs     = [`locale/`]
    includeSrc  = true
    podFacets   = ["indexFacets":["uriScheme"]]
  }

  @target="compile fan source into pod"
  override Void compile(Bool full := true)
  {
    super.compile(full)
  }

}