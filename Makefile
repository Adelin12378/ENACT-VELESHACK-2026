.ONESHELL:
setup:
	echo "Creating kind cluster..."
	kind create cluster --config=cluster-config/kind-config.yaml

	echo "Setting context to kind cluster..."
	kubectl config use-context kind-enact-dev

	kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

	echo "Installing Cilium CNI"
	helm repo add cilium https://helm.cilium.io/ --force-update
	helm repo add prometheus-community https://prometheus-community.github.io/helm-charts   --force-update
	helm repo add enact-tdcme https://gitlab.eclipse.org/api/v4/projects/8265/packages/helm/stable --force-update
	helm repo add enact-applpm https://gitlab.eclipse.org/api/v4/projects/8268/packages/helm/stable --force-update
	helm repo add kepler https://sustainable-computing-io.github.io/kepler-helm-chart --force-update

	helm repo update
	helm install cilium cilium/cilium --namespace kube-system --version 1.20.1 \
	--set cluster.name="cloud1" \
	--set cluster.id=1 \
	--set hubble.enabled=true \
	--set hubble.tls.enabled=false \
	--set hubble.relay.enabled=true \
	--set hubble.ui.enabled=true \
	--set hubble.metrics.enableOpenMetrics=true \
	--set hubble.metrics.enabled="{dns,drop,tcp,flow,port-distribution,icmp,httpV2:exemplars=true;labelsContext=source_ip\,source_namespace\,source_workload\,destination_ip\,destination_namespace\,destination_workload\,traffic_direction}" \
	--set global.hubble.enabled=true \
	--set global.hubble.listenAddress=":4244" \
	--set global.hubble.ui.enabled=true \
	--set envoy.enabled=true \
	--set prometheus.enabled=true \
	--set operator.prometheus.enabled=true \
	--set cni.chainingMode="none"

	echo "Installing ENACT Core components under enact namespace"
	kubectl create namespace enact
	## workaround for EDC
	kubectl create secret generic edc-api-key-secret -n enact --from-literal=EDC_API_KEY="" 

	helm install infra -n enact oci://ghcr.io/prometheus-community/charts/kube-prometheus-stack
	helm install kepler kepler/kepler --namespace enact --create-namespace \
    --set serviceMonitor.enabled=true \
    --set serviceMonitor.labels.release=infra

	helm install tdcme-api enact-tdcme/monitor-api --namespace enact \
	--set existingSecret="" \
	--set secretKey=""

	TOKEN=$$(kubectl get secret join-token-secret -n enact -o jsonpath="{.data.token}" | base64 -d)
	echo "Token: $$TOKEN"
	helm install tdcme-agent enact-tdcme/monitor-api-agent --namespace enact \
	--set api.host="http://monitor-api-service.enact.svc.cluster.local" \
	--set api.joinToken="$$TOKEN" \
	--set cluster.name="dev" \
	--set agent.prometheusHost="http://infra-kube-prometheus-stac-prometheus.enact.svc.cluster.local:9090" \

	helm install applpm enact-applpm/appl --namespace enact
    
	echo "Waiting for all Pods to start"
	kubectl wait --for=condition=ready pod -n enact --all --timeout=120s
	echo "Labeling nodes"
 
	curl -X POST http://0.0.0.0:35580/api/v1/nodes/enact-dev-worker/labels \
	  -H "Content-Type: application/json" \
	  -d '{
	    "add": {
	      "enact.eu/green-ratio": "0.85",
	      "enact.eu/role": "edge",
	      "enact.eu/region": "eu-west-1",
	      "enact.eu/zone": "eu-west-1a"
	    }
	  }'

	curl -X POST http://0.0.0.0:35580/api/v1/nodes/enact-dev-worker2/labels \
	  -H "Content-Type: application/json" \
	  -d '{
	    "add": {
	      "enact.eu/green-ratio": "0.9",
	      "enact.eu/role": "cloud",
	      "enact.eu/region": "eu-west-2",
	      "enact.eu/zone": "eu-west-2a"
	    }
	  }'


	echo "All ENACT components have been installed. Refer to ENACT Eclipse Gitlab space for component documentation: https://gitlab.eclipse.org/eclipse-research-labs/enact-project"

clean:
	echo "Cleaning up..."
	kind delete cluster --name enact-dev

	echo "Cleaning Helm repositories..."
	helm repo remove prometheus-community
	helm repo remove enact-tdcme
	helm repo remove enact-applpm
	helm repo remove cilium
	docker system prune -f