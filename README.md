# CompaaS

## Important Commands

``` bash
sbt --client -J-Xmx2G "~Docker / publishLocal"
kubectl apply -k k8s/overlays/minikube
kubectl rollout restart deployment compaas -n compaas
```

## Contributors

- [Audacious Tux](//audacioustux.com)
