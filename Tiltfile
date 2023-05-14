local_resource(
    "sbt", 
    serve_cmd='sbt -J-Xmx2G "~stage"', 
    deps=["build.sbt"]
)

docker_build(
  "compaas", 
  context="target/universal/stage", 
  dockerfile="Dockerfile",
  build_args={}
)
docker_prune_settings(True)

k8s_yaml(kustomize("k8s/compaas/overlays/dev"))

local_resource(
  "minikube-tunnel",
  serve_cmd="minikube tunnel --bind-address 0.0.0.0",
)

local_resource(
  "minikube-dashboard",
  serve_cmd="minikube dashboard",
)

analytics_settings(enable=False)
