import * as k8s from "@pulumi/kubernetes";

const argocd_ns = new k8s.core.v1.Namespace("argocd", {
    metadata: { name: "argocd" },
});

// TODO: manage argocd with argocd
const argo_cd = new k8s.yaml.ConfigFile("argocd", {
    file: "https://raw.githubusercontent.com/argoproj/argo-cd/master/manifests/install.yaml",
    transformations: [
        (obj: any) => {
            if (obj.metadata) {
                obj.metadata.namespace = argocd_ns.metadata.name;
            }
        }
    ],
});

// TODO: use ApplicationSet
// apply all yaml files in apps directory
const apps = new k8s.yaml.ConfigGroup("apps", {
    files: "apps/*.yaml",
    transformations: [
        (obj: any) => {
            if (obj.metadata) {
                obj.metadata.namespace = argocd_ns.metadata.name;
            }
        }
    ],
}, { dependsOn: argo_cd });