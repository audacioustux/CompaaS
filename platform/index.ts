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

const yugabyte = new k8s.helm.v3.Release("yugabyte", {
    chart: "yugabyte",
    repositoryOpts: {
        repo: "https://charts.yugabyte.com",
    },
    namespace: "yugabyte",
    createNamespace: true,
    values: {
        resource: {
            master: {
                requests: {
                    cpu: "0.5",
                    memory: "0.5Gi",
                },
            },
            tserver: {
                requests: {
                    cpu: "0.5",
                    memory: "0.5Gi",
                },
            },
        },
        replicas: {
            master: 1,
            tserver: 1,
        },
    },
});

const kubevela = new k8s.helm.v3.Release("kubevela", {
    chart: "vela-core",
    repositoryOpts: {
        repo: "https://charts.kubevela.net/core",
    },
    namespace: "vela-system",
    createNamespace: true,
    values: {
        admissionWebhooks: {
            certManager: {
                enabled: true,
            },
        },
    },
}, { dependsOn: [certManager] });