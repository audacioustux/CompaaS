load('ext://helm_remote', 'helm_remote')

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
          
helm_remote(
  chart="yugabyte",
  repo_url="https://charts.yugabyte.com",
  namespace="yugabyte",
  values="k8s/yugabyte/values.yaml",
  create_namespace=True,
)

k8s_yaml(kustomize("k8s/compaas/overlays/dev"))

docker_prune_settings(True)
analytics_settings(enable=False)
