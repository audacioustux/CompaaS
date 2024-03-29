load('ext://helm_resource', 'helm_resource', 'helm_repo')

def watch_kustomize(pathToDir, **kwargs):
  watch_file(pathToDir)
  return kustomize(pathToDir, **kwargs)

def deploy_yugabyte():
  namespace = "yugabyte"
  ysqlsh = "kubectl exec -n {namespace} -it yb-tserver-0 -- ysqlsh".format(namespace=namespace)

  helm_repo(
    'yugabyte', 
    resource_name='yugabyte-repo',
    url='https://charts.yugabyte.com'
  )
  helm_resource(
    'yugabyte', 
    chart='yugabyte/yugabyte', 
    namespace=namespace,
    flags=['--values=k8s/yugabyte/values.yaml', '--create-namespace'],
    resource_deps=["yugabyte-repo"]
  )
  local_resource(
    "wait-for-yugabyte",
    cmd="ebort -- {ysqlsh} -c 'select 1;' >> /dev/null 2>&1 && echo 'Yugabyte is ready'".format(ysqlsh=ysqlsh),
    resource_deps=["yugabyte"]
  )
  local_resource(
    "provision-yugabyte",
    dir="k8s/yugabyte",
    cmd="{ysqlsh} < ddl-scripts/akka-r2dbc_up.sql".format(ysqlsh=ysqlsh),
    resource_deps=["wait-for-yugabyte"]
  )

def deploy_compaas():
  local_resource(
    "sbt", 
    serve_cmd='sbt -J-Xmx2G "~stage"', 
    deps=["build.sbt", "project/*"]
  )
  docker_build(
    "compaas", 
    context="target/universal/stage", 
    dockerfile="Dockerfile"
  )
  k8s_yaml(watch_kustomize("k8s/compaas/overlays/dev"))

deploy_yugabyte()
deploy_compaas()

docker_prune_settings(True)
analytics_settings(enable=False)
