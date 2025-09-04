output "jenkins_master_public_ip" {
  value = aws_instance.jenkins_master.public_ip
}

output "sonarqube_server_public_ip" {
  value = aws_instance.sonarqube_server.public_ip
}

output "k8s_master_public_ip" {
  value = aws_instance.k8s_master.public_ip
}

output "k8s_worker_public_ips" {
  value = aws_instance.k8s_worker[*].public_ip
}