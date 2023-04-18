# if k8s_namespace() == 'default':
#   fail("failing early to avoid deploying to 'default' namespace")

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

k8s_yaml(
  kustomize("k8s/overlays/dev")
)

local_resource(
  "minikube-tunnel",
  serve_cmd="minikube tunnel --bind-address 0.0.0.0",
)

analytics_settings(enable=False)
