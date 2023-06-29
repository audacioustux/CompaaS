import { Namespace } from "@pulumi/kubernetes/core/v1";

function useNamespace(namespace: Namespace) {
    return (obj: any) => {
        if (obj.metadata) {
            obj.metadata.namespace = namespace;
        }
    };
}

export { useNamespace };