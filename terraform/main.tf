
resource "aws_security_group" "ecs_sg" {
  name        = "framedrop-ecs-sg"
  description = "Security group for ECS tasks - allows traffic from ALB"
  vpc_id      = data.aws_vpc.vpc_default.id

  ingress {
    description     = "Allow traffic from ALB on port 8080"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [data.aws_security_group.alb_sg.id]
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "framedrop-ecs-sg"
    Environment = "dev"
  }
}


resource "aws_ecs_task_definition" "framedrop_upload_app" {
  family                   = "framedrop-upload-task-definition"
  cpu                      = "256"
  memory                   = "512"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  execution_role_arn       = data.aws_iam_role.lab_role.arn
  task_role_arn            = data.aws_iam_role.lab_role.arn
  container_definitions = jsonencode([{
    name      = "framedrop-upload-app"
    image     = "${data.aws_ecr_repository.framedrop_upload_app_repo.repository_url}:latest"
    essential = true
    portMappings = [{
      containerPort = 8080
      hostPort      = 8080

    }]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.framedrop_upload_app_logs.name
        "awslogs-region"        = "us-east-1"
        "awslogs-stream-prefix" = "ecs"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1"]
      interval    = 60
      timeout     = 5
      retries     = 5
      startPeriod = 120
    }

    environment = [
      {
        name  = "AWS_REGION"
        value = "us-east-1"
      }
    ]

    secrets = [
      {
        name      = "S3_BUCKET_NAME"
        valueFrom = data.aws_ssm_parameter.bucket_name.arn
      },
      {
        name      = "SQS_VIDEO_PROCESSING_QUEUE_URL"
        valueFrom = data.aws_ssm_parameter.sqs_video_processing_queue_url.arn
      }
    ]
    

  }])

}


resource "aws_ecs_service" "framedrop_upload_app_service" {
  name            = "framedrop-upload-app-service"
  cluster         = data.aws_ecs_cluster.ecs_framedrop_upload_cluster.id
  task_definition = aws_ecs_task_definition.framedrop_upload_app.arn
  desired_count   = 1
  launch_type     = "FARGATE"
  force_new_deployment = true

  network_configuration {
    subnets          = data.aws_subnets.aws_subnets_default.ids
    security_groups  = [aws_security_group.ecs_sg.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = data.aws_lb_target_group.framedrop_upload_lb_target_group.arn
    container_name   = "framedrop-upload-app"
    container_port   = 8080
  }

  deployment_minimum_healthy_percent = 50
  deployment_maximum_percent         = 200

}


resource "aws_cloudwatch_log_group" "framedrop_upload_app_logs" {
  name              = "/ecs/framedrop-upload-task-family"
  retention_in_days = 1

  tags = {
    Name        = "framedrop-upload-logs"
    Environment = "dev"
  }
}