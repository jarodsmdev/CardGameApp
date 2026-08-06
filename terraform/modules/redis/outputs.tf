output "redis_host" {
  value = aws_elasticache_replication_group.this.primary_endpoint_address
}

output "redis_port" {
  value = aws_elasticache_replication_group.this.port
}
