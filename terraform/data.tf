data "aws_ecs_cluster" "ecs_framedrop_upload_cluster" {
  cluster_name = "framedrop-ecs-cluster"
}

data "aws_iam_role" "ecs_task_execution_role" {
  name = "AWSServiceRoleForECS"
}

data "aws_iam_role" "lab_role" {
  name = "LabRole"
}

data "aws_ecr_repository" "framedrop_upload_app_repo" {
  name = "framedrop-upload-app"
}

data "aws_vpc" "vpc_default" {
  default = true
}


data "aws_subnets" "aws_subnets_default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.vpc_default.id]
  }

  filter {
    name   = "default-for-az"
    values = ["true"]
  }
}

data "aws_security_group" "alb_sg" {
  filter {
    name   = "group-name"
    values = ["framedrop-alb-sg"]
  }

  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.vpc_default.id]
  }
}

data "aws_lb_target_group" "framedrop_upload_lb_target_group" {
  name = "framedrop-alb-tg"
}

data "aws_ssm_parameter" "bucket_name" {
  name = "/framedrop/infra/bucket_name"
}

data "aws_ssm_parameter" "sqs_video_processing_queue_url" {
  name = "/framedrop/infra/sqs_video_processing_queue_url"
}