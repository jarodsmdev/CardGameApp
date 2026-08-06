resource "aws_security_group" "this" {
  name        = "${var.name_prefix}-${var.name}"
  description = var.description
  vpc_id      = var.vpc_id

  dynamic "ingress" {
    for_each = var.ingress_rules
    content {
      from_port       = ingress.value.from_port
      to_port         = ingress.value.to_port
      protocol        = ingress.value.protocol
      cidr_blocks     = try(ingress.value.cidr_blocks, null)
      security_groups = try(ingress.value.security_groups, null)
      description     = try(ingress.value.description, null)
    }
  }

  dynamic "egress" {
    for_each = var.egress_rules
    content {
      from_port   = egress.value.from_port
      to_port     = egress.value.to_port
      protocol    = egress.value.protocol
      cidr_blocks = try(egress.value.cidr_blocks, ["0.0.0.0/0"])
      description = try(egress.value.description, null)
    }
  }

  tags = merge(var.tags, { Name = "${var.name_prefix}-${var.name}" })
}
