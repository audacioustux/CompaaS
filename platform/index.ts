import * as k8s from "@pulumi/kubernetes";
import { Namespace } from "@pulumi/kubernetes/core/v1";

function useNamespace(namespace: Namespace) {
    return (obj: any) => {
        if (obj.metadata) {
            obj.metadata.namespace = namespace;
        }
    };
}

const argocd_ns = new k8s.core.v1.Namespace("argocd-ns", {
    metadata: { name: "argocd" },
});

const argocd = new k8s.kustomize.Directory("argocd", {
    directory: "apps/argocd",
    transformations: [useNamespace(argocd_ns)],
});

const apps = new k8s.yaml.ConfigGroup("apps", {
    files: "apps/*.yaml",
    transformations: [useNamespace(argocd_ns)],
}, { dependsOn: argocd });