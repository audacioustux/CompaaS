PGPASSWORD="password" psql --host 127.0.0.1 -U postgres -d postgres -p 5432 --single-transaction -f assets/ddl-scripts/akka-r2dbc_up.sql
kubectl port-forward --namespace compaas-dev svc/postgres 5432:5432 &