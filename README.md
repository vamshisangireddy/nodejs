# End-to-End DevOps Project

This project contains a Node.js application and the full infrastructure, pipeline, and configuration code to build, test, and deploy it to a Kubernetes cluster on AWS.

## Project Structure

- **/app**: The Node.js application source code.
- **/infrastructure**: Contains Terraform and Ansible code for IaC.
- **/kubernetes**: Kubernetes deployment and service manifests.
- **/monitoring**: Configuration files for Prometheus and Logstash.
- **Jenkinsfile**: The CI/CD pipeline definition.

## Order of Operations

1.  **Prerequisites:**
    * An AWS account with credentials configured locally.
    * An EC2 Key Pair named `my-aws-key` created in your desired AWS region, with the private key located at `~/.ssh/my-aws-key.pem`.
    * Terraform and Ansible installed locally.
    * Jenkins installed and running on your local machine.
    * A Docker Hub account.

2.  **Provision Cloud Infrastructure (Terraform):**
    * Navigate to `infrastructure/terraform/`.
    * Run `terraform init`.
    * Run `terraform plan`.
    * Run `terraform apply`.
    * Note the IP addresses from the output.

3.  **Configure Servers (Ansible):**
    * Update the `infrastructure/ansible/inventory/hosts` file with the IP addresses from the Terraform output.
    * Run the playbooks in order:
        ```bash
        # 1. Setup SonarQube
        ansible-playbook -i infrastructure/ansible/inventory/hosts infrastructure/ansible/playbooks/setup-sonarqube.yml

        # 3. Setup the Kubernetes cluster
        ansible-playbook -i infrastructure/ansible/inventory/hosts infrastructure/ansible/playbooks/setup-k8s-cluster.yml
        
        # 4. Automate Jenkins K8s Credential Setup
        ansible-playbook -i infrastructure/ansible/inventory/hosts infrastructure/ansible/playbooks/configure-jenkins-credentials.yml
        ```

4.  **Configure Jenkins:**
    * Access your local Jenkins at `http://localhost:8080`.
    * Complete the initial setup.
    * Install the necessary plugins (`NodeJS`, `SonarQube Scanner`, `Docker Pipeline`, `Credentials Binding`, etc.).
    * Add your AWS, SonarQube, and Docker Hub credentials in **Manage Jenkins -> Credentials**.
    * Configure SonarQube server in **Manage Jenkins -> Configure System**.
    * Configure NodeJS tool in **Manage Jenkins -> Global Tool Configuration**.
    * The `configure-jenkins-credentials.yml` playbook will have already added the Kubernetes credentials.
    * Create a new **Pipeline** job and point it to your Git repository.

5.  **Run the Pipeline:**
    * Trigger the Jenkins pipeline. It will build, test, analyze, and deploy the application to your Kubernetes cluster.

6.  **Access the Application:**
    * You can access your application at `http://<any_k8s_node_ip>:30001`.