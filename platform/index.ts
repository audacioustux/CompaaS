import * as k8s from "@pulumi/kubernetes";
import * as kx from "@pulumi/kubernetesx";

const certManager = new k8s.helm.v3.Release("cert-manager", {
    chart: "cert-manager",
    repositoryOpts: {
        repo: "https://charts.jetstack.io",
    },
    namespace: "cert-manager",
    createNamespace: true,
    values: {
        installCRDs: true,
    },
});