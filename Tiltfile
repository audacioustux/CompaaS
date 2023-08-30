load('ext://helm_resource', 'helm_resource', 'helm_repo')

helm_repo(
  'yugabyte', 
  resource_name='yugabyte-repo',
  url='https://charts.yugabyte.com'
)
helm_resource(
  'yugabyte', 
  chart='yugabyte/yugabyte', 
  namespace='yugabyte', 
  flags=['--values=k8s/yugabyte/values.yaml', '--create-namespace'],
  resource_deps=["yugabyte-repo"]
)
local_resource(
  "wait-for-yugabyte",
  dir="scripts",
  cmd="./ebort.sh -- kubectl exec -n yugabyte -it yb-tserver-0 -- ysqlsh -c 'select 1;' >> /dev/null 2>&1 && echo 'Yugabyte is ready'",
  resource_deps=["yugabyte"]
)
local_resource(
  "provision-yugabyte",
  dir="k8s/yugabyte",
  cmd="kubectl exec -n yugabyte -it yb-tserver-0 -- ysqlsh < ddl-scripts/akka-r2dbc_up.sql",
  resource_deps=["wait-for-yugabyte"]
)

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
