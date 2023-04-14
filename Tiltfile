local_resource(
    "sbt", 
    serve_cmd='sbt --client -J-Xmx2G "~Docker / stage"', 
    deps=["build.sbt"]
)

docker_build("compaas", "target/docker/stage")
docker_prune_settings(True)

k8s_yaml(
  kustomize("k8s/overlays/dev")
)