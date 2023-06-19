local_resource(
    "sbt", 
    serve_cmd='sbt -J-Xmx2G "~stage"', 
    deps=["build.sbt"]
)

docker_build(
  "compaas", 
  context="target/universal/stage", 
  dockerfile="Dockerfile"
)

docker_prune_settings(True)
analytics_settings(enable=False)
