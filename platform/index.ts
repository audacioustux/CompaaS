import * as k8s from "@pulumi/kubernetes";
import { useNamespace } from "./utils";

const argocd_ns = new k8s.core.v1.Namespace("argocd-ns", {
    metadata: { name: "argocd" },
});

const argocd = new k8s.kustomize.Directory("argocd", {
    directory: "apps/argocd",
    transformations: [useNamespace(argocd_ns)],
});


// const argocd_app = new k8s.apiextensions.CustomResource("argocd-app", {
//     apiVersion: "argoproj.io/v1alpha1",
//     kind: "Application",
//     metadata: { name: "argocd" },
//     spec: {
//         destination: {
//             namespace: "argocd",
//             server: "https://kubernetes.default.svc",
//         },
//         project: "default",
//         source: {
//             path: "platform/apps/argocd",
//             repoURL: "https://github.com/audacioustux/CompaaS"
//         },
//         syncPolicy: {
//             automated: {
//                 prune: true,
//                 selfHeal: true,
//             },
//             syncOptions: ["CreateNamespace=true"],
//         },
//     },
// }, { dependsOn: [argocd] });
