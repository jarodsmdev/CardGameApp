resource "aws_db_subnet_group" "this" {
  name       = "${var.name_prefix}-db-subnet-group"
  subnet_ids = var.subnet_ids

  tags = merge(var.tags, { Name = "${var.name_prefix}-db-subnet-group" })
}

resource "aws_db_instance" "this" {
  identifier     = "${var.name_prefix}-db"
  engine         = "postgres"
  engine_version = var.engine_version

  instance_class    = var.instance_class
  allocated_storage = var.allocated_storage
  storage_encrypted = true
  storage_type      = "gp3"
  multi_az          = var.multi_az

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = var.security_group_ids

  backup_retention_period   = var.backup_retention_period
  skip_final_snapshot       = false
  final_snapshot_identifier = "${var.name_prefix}-final"

  tags = merge(var.tags, { Name = "${var.name_prefix}-db" })
}
