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

k8s_yaml(kustomize("k8s/compaas/overlays/dev"))

docker_prune_settings(True)
analytics_settings(enable=False)
