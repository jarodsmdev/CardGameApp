resource "aws_elasticache_subnet_group" "this" {
  name       = "${var.name_prefix}-redis-subnet-group"
  subnet_ids = var.subnet_ids
}

resource "aws_elasticache_replication_group" "this" {
  replication_group_id       = "${var.name_prefix}-redis"
  description                = "Redis (sesiones/caché) para ${var.name_prefix}"
  engine                     = "redis"
  engine_version             = var.engine_version
  node_type                  = var.node_type
  num_cache_clusters         = var.num_cache_clusters
  automatic_failover_enabled = var.num_cache_clusters > 1
  port                       = 6379
  parameter_group_name       = var.parameter_group_name

  subnet_group_name  = aws_elasticache_subnet_group.this.name
  security_group_ids = var.security_group_ids

  tags = merge(var.tags, { Name = "${var.name_prefix}-redis" })
}
