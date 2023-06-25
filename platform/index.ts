import * as k8s from "@pulumi/kubernetes";

const argocd_ns = new k8s.core.v1.Namespace("argocd", {
    metadata: { name: "argocd" },
});

const argocd = new k8s.yaml.ConfigFile("argocd", {
    file: "https://raw.githubusercontent.com/argoproj/argo-cd/master/manifests/ha/install.yaml",
    transformations: [
        (obj: any) => {
            if (obj.metadata) {
                obj.metadata.namespace = argocd_ns.metadata.name;
            }
        }
    ],
});