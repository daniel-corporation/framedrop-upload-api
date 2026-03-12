# FrameDrop - Upload API

## Visão Geral

O **FrameDrop Upload API** é um microserviço de upload e gerenciamento de vídeos desenvolvido como parte do ecossistema FrameDrop. Este serviço é responsável por receber uploads de vídeos via API REST, armazenar no S3, persistir metadados no DynamoDB, gerar URLs pré-assinadas para download e enviar mensagens para a fila SQS para processamento assíncrono pelo serviço de processamento de vídeos.

## Arquitetura

O projeto segue os princípios da **Hexagonal Architecture** (Arquitetura Hexagonal / Ports & Adapters), promovendo separação de responsabilidades e facilitando manutenção e testes.

### Camadas da Aplicação

```
┌─────────────────────────────────────────────────────────┐
│                     Adapters In                         │
│           (REST Controllers & DTOs)                     │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────┐
│                   Core - Ports In                       │
│              (Input Port Interfaces)                    │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────┐
│              Core - Application Layer                   │
│                   (Use Cases)                           │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────┐
│                Core - Domain Layer                      │
│          (Entities, Enums & Business Rules)             │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────┐
│                  Core - Ports Out                       │
│            (Output Port Interfaces)                     │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────┐
│                    Adapters Out                         │
│      (S3, DynamoDB, SQS, Tika, JWT)                     │
└─────────────────────────────────────────────────────────┘
```

### Estrutura de Pacotes

```
com.framedrop.upload_api
├── UploadApiApplication.java
├── adapters
│   ├── config
│   │   ├── DynamoDbConfig.java          # DynamoDB client e table beans
│   │   ├── SqsConfig.java               # SqsClient bean
│   │   ├── TokenConfig.java             # JWT secret configuration
│   │   └── UploadConfig.java            # Wiring de use cases e adapters
│   ├── in
│   │   └── controller
│   │       ├── UploadController.java    # POST /api/uploads (multipart upload)
│   │       ├── VideoController.java     # GET /api/videos/users/{id}, PATCH /api/videos/{id}
│   │       └── dto
│   │           ├── UserDTO.java         # Record: userId, userName, email
│   │           ├── VideoDTO.java        # Record completo de Video com presigned URL
│   │           └── VideoStatusDTO.java  # Record: status
│   └── out
│       ├── UploadS3Adapter.java         # Upload de vídeo para o S3
│       ├── PreSignedUrlS3Adapter.java   # Geração de URL pré-assinada
│       ├── ValidateVideoAdapter.java    # Validação MIME type com Apache Tika
│       ├── SqsVideoQueueAdapter.java    # Envio de mensagens para fila SQS
│       ├── dynamodb
│       │   ├── VideoDynamoAdapter.java  # CRUD de vídeos no DynamoDB
│       │   └── entity
│       │       └── VideoEntity.java     # Entidade mapeada para DynamoDB
│       └── dto
│           └── VideoMetadata.java       # Record de mensagem para SQS
└── core
    ├── application
    │   └── usecases
    │       ├── UploadVideoUseCase.java  # Orquestração do upload de vídeo
    │       ├── VideoUseCase.java        # Listagem e atualização de status
    │       └── TokenUseCase.java        # Validação e extração de dados do JWT
    └── domain
        ├── model
        │   ├── Video.java               # Entidade de domínio com validação
        │   └── enums
        │       └── StatusProcess.java   # PENDING, PROCESSING, COMPLETED, FAILED
        └── ports
            ├── in
            │   ├── UploadVideoInputPort.java
            │   ├── VideoInputPort.java
            │   ├── TokenInputPort.java
            │   └── ValidateVideoInputPort.java
            └── out
                ├── UploadVideoOutputPort.java
                ├── VideoOutputPort.java
                ├── PreSignedUrlOutputPort.java
                ├── ValidateVideoOutputPort.java
                └── VideoProcessQueueOutPut.java
```

## Tecnologias Utilizadas

### Backend Framework
- **Java 25**
- **Spring Boot 4.0.2**
- **Spring Web MVC** - Controllers REST
- **Spring Validation** - Validação de entrada
- **Spring Actuator** - Health checks e métricas
- **Lombok** - Redução de boilerplate

### AWS Services
- **Amazon S3** - Armazenamento de vídeos e arquivos processados
- **Amazon DynamoDB** - Persistência de metadados dos vídeos
- **Amazon SQS** - Fila de mensagens para processamento assíncrono
- **AWS SDK 2.20.0/2.20.21** - SDK para integração com serviços AWS

### Segurança
- **Auth0 Java JWT 4.5.0** - Validação e parsing de tokens JWT

### Validação de Vídeo
- **Apache Tika 3.2.3** - Detecção e validação de MIME types

### Testes
- **JUnit 5** - Framework de testes
- **Mockito** - Mocks e testes unitários
- **Spring Boot Test** - Testes de integração

### Build & Deploy
- **Maven** - Gerenciador de dependências
- **Docker** - Containerização
- **Terraform** - Infrastructure as Code (AWS)

## Funcionalidades Principais

### 1. Upload de Vídeo

O pipeline de upload é orquestrado pelo `UploadVideoUseCase` e segue os seguintes passos:

1. **Validação do token JWT** - Extrai informações do usuário (userId, userName)
2. **Validação do formato** - Verifica MIME type do arquivo usando Apache Tika
3. **Criação da entidade de domínio** - `Video` com validações de negócio
4. **Persistência no DynamoDB** - Salva metadados do vídeo com status `PENDING`
5. **Upload para S3** - Armazena vídeo em `videos/{userId}/{timestamp}_{fileName}`
6. **Enfileiramento para processamento** - Envia mensagem SQS com metadados do vídeo
7. Em caso de erro → retorna erro apropriado (400 para formato inválido, 500 para erro interno)

### 2. Endpoints REST

#### Upload de Vídeo
- **Endpoint**: `POST /api/uploads`
- **Content-Type**: `multipart/form-data`
- **Headers**: `Authorization: Bearer {token}`
- **Body**:
  - `videoFile`: arquivo de vídeo (MultipartFile)
  - `email`: email do usuário (validado)
- **Resposta Success**: `200 OK - "Video was sent to processing"`
- **Resposta Erro**: `400 Bad Request` ou `500 Internal Server Error`

#### Listar Vídeos do Usuário
- **Endpoint**: `GET /api/videos/users/{id}`
- **Descrição**: Retorna lista de vídeos de um usuário com URLs pré-assinadas
- **Resposta**: Array de `VideoDTO`

#### Atualizar Status do Vídeo
- **Endpoint**: `PATCH /api/videos/{id}`
- **Descrição**: Atualiza status do vídeo (chamado pelo serviço de processamento)
- **Body**: `{ "status": "PROCESSING" | "COMPLETED" | "FAILED" }`
- **Comportamento especial**: 
  - Quando status é `COMPLETED`, gera URL pré-assinada para o ZIP processado
  - Calcula path do ZIP: `processed/{userId}/{videoId}_{timestamp}_{fileName}_frames.zip`

### 3. Integração JWT

#### Validação de Token
- Extrai token do header `Authorization` (formato Bearer)
- Valida assinatura JWT usando secret configurado
- Extrai claims: `userId`, `userName`
- Retorna `UserDTO` para uso nos use cases

### 4. Integrações Externas

#### Amazon S3
- Upload de vídeos originais para `videos/{userId}/`
- Geração de URLs pré-assinadas para download de ZIPs processados
- Bucket configurável via variável de ambiente

#### Amazon DynamoDB
- Tabela: configurável via variável de ambiente
- Chave primária: `videoId`
- Atributos: userId, userName, email, videoPath, fileName, fileExtension, dateUploaded, statusProcess, urlPreSigned

#### Amazon SQS
- Envio de mensagens com metadados do vídeo para fila de processamento
- Formato: `VideoMetadata(videoId, userId, email, filePath, status)`

## Casos de Uso Implementados

| Use Case | Descrição |
|----------|-----------|
| `UploadVideoUseCase` | Orquestração completa do upload, validação e enfileiramento |
| `VideoUseCase` | Listagem de vídeos por usuário e atualização de status |
| `TokenUseCase` | Validação e extração de dados do JWT |

## Ports (Interfaces)

### Input Ports

| Port | Método |
|------|--------|
| `UploadVideoInputPort` | `void uploadVideo(MultipartFile videoFile, UserDTO userDto)` |
| `VideoInputPort` | `List<VideoDTO> getAllVideosByUserId(String userId)` |
| | `void updateVideoStatus(String videoId, String status)` |
| `TokenInputPort` | `UserDTO getUserFromToken(String bearerToken)` |

### Output Ports

| Port | Método |
|------|--------|
| `UploadVideoOutputPort` | `void uploadVideoToStorage(String videoPath, MultipartFile file)` |
| `VideoOutputPort` | `List<Video> getVideosByUserId(String userId)` |
| | `Video getVideoById(String videoId)` |
| | `void save(Video video)` |
| `PreSignedUrlOutputPort` | `String generatePreSignedUrl(String objectKey)` |
| `ValidateVideoOutputPort` | `boolean isValidFormatVideo(MultipartFile file)` |
| `VideoProcessQueueOutPut` | `void pushToQueue(VideoMetadata videoMetadata)` |

## Modelo de Dados

### Entidade: Video

```java
{
  "videoId": "String (UUID)",
  "userId": "String",
  "userName": "String",
  "email": "String",
  "videoPath": "String",
  "fileName": "String",
  "fileExtension": "String",
  "dateUploaded": "LocalDateTime",
  "statusProcess": "StatusProcess",
  "urlPreSigned": "String (nullable)"
}
```

### Extensões Permitidas
`.mp4`, `.mkv`, `.webm`, `.mov`, `.avi`

### Enum: StatusProcess

| Status | Descrição |
|--------|-----------|
| `PENDING` | Aguardando processamento |
| `PROCESSING` | Em processamento |
| `COMPLETED` | Processamento concluído |
| `FAILED` | Falha no processamento |

## Padrões de Status de Processamento

```
PENDING → PROCESSING → COMPLETED
                    └→ FAILED
```

## Configuração

### Variáveis de Ambiente

```properties
# AWS S3
S3_BUCKET_NAME=<bucket_name>

# AWS SQS
SQS_VIDEO_PROCESSING_QUEUE_URL=<queue_url>

# JWT Secret
JWT_SECRET=<secret_key>

# AWS Region
AWS_REGION=us-east-1

# DynamoDB (se necessário override)
DYNAMODB_TABLE_NAME=<table_name>
```

### application.properties

```properties
spring.application.name=upload-api
server.port=8080

# AWS Configuration
aws.s3.bucket-name=${S3_BUCKET_NAME:framedrop-upload-tst}
aws.sqs.video-processing-queue-url=${SQS_VIDEO_PROCESSING_QUEUE_URL:https://sqs.us-east-1...}
aws.region=us-east-1

# Multipart Configuration (max 100MB)
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
spring.servlet.multipart.enabled=true
```

## Docker

O projeto inclui um `Dockerfile` para containerização da aplicação.

### Build da Imagem
```bash
cd upload-api
docker build -t framedrop-upload-api:latest .
```

### Executar Container
```bash
docker run -p 8080:8080 \
  -e S3_BUCKET_NAME=<bucket> \
  -e SQS_VIDEO_PROCESSING_QUEUE_URL=<queue_url> \
  -e JWT_SECRET=<secret> \
  -e AWS_ACCESS_KEY_ID=<key> \
  -e AWS_SECRET_ACCESS_KEY=<secret> \
  framedrop-upload-api:latest
```

## Infraestrutura (Terraform)

O diretório `/terraform` contém a infraestrutura como código para provisionar recursos na AWS.

### Recursos Criados
- **Security Group** - Configuração de rede para ECS
- **ECS Task Definition** - Definição da tarefa Fargate
- **ECS Service** - Serviço ECS com integração ALB
- **CloudWatch Log Group** - Logs da aplicação

### Deploy da Infraestrutura
```bash
cd terraform
terraform init
terraform plan
terraform apply
```

## Testes

### Executar Testes Unitários
```bash
cd upload-api
mvn test
```

### Estrutura de Testes

- **Unit Tests**: Testes unitários de todos os adapters, use cases e modelos de domínio
- **Mocking**: JUnit 5 + Mockito
- **Controller Tests**: Testes de endpoints REST

### Classes de Teste

| Classe | Descrição |
|--------|-----------|
| `UploadApiApplicationTests` | Teste de contexto da aplicação |
| `UploadControllerTest` | Testes do endpoint de upload |
| `VideoControllerTest` | Testes dos endpoints de vídeo |
| `UploadS3AdapterTest` | Upload de vídeos para o S3 |
| `PreSignedUrlS3AdapterTest` | Geração de URLs pré-assinadas |
| `ValidateVideoAdapterTest` | Validação de formato de vídeo |
| `SqsVideoQueueAdapterTest` | Envio de mensagens para SQS |
| `VideoDynamoAdapterTest` | Operações CRUD no DynamoDB |
| `UploadVideoUseCaseTest` | Cenários de upload |
| `VideoUseCaseTest` | Cenários de listagem e atualização |
| `TokenUseCaseTest` | Validação de JWT |
| `VideoTest` | Validação de domínio |

## Monitoramento

### Spring Actuator

A aplicação expõe endpoints de monitoramento:

- **Health Check**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Info**: `GET /actuator/info`

## Padrões de Código

- **Hexagonal Architecture (Ports & Adapters)**: Separação clara entre core e infraestrutura
- **SOLID Principles**: Código modular e testável
- **Dependency Injection**: Gerenciado pelo Spring
- **DTOs**: Separação entre camadas de entrada/saída
- **Port Pattern**: Abstrações para integrações externas (S3, DynamoDB, SQS)
- **Domain Validation**: Validações de negócio encapsuladas na entidade de domínio

## Como Executar Localmente

### Pré-requisitos
- Java 25
- Maven 3.x
- Docker (opcional)
- AWS CLI configurado (para S3, DynamoDB e SQS)

### Passos

1. **Clone o repositório**
```bash
git clone https://github.com/daniel-dev-vs/framedrop-upload-api.git
cd framedrop-upload-api
```

2. **Configure as variáveis de ambiente**
```bash
export S3_BUCKET_NAME=your_bucket
export SQS_VIDEO_PROCESSING_QUEUE_URL=your_queue_url
export JWT_SECRET=your_jwt_secret
```

3. **Build do projeto**
```bash
cd upload-api
mvn clean install
```

4. **Execute a aplicação**
```bash
mvn spring-boot:run
```

5. **Verifique o health check**
```
http://localhost:8080/actuator/health
```

6. **Teste o upload (exemplo com curl)**
```bash
curl -X POST http://localhost:8080/api/uploads \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "videoFile=@/path/to/video.mp4" \
  -F "email=user@example.com"
```

## Comunicação entre Microserviços

### Fluxo Completo

1. **Upload API** recebe vídeo via REST
2. **Upload API** armazena no S3 e DynamoDB
3. **Upload API** envia mensagem para SQS
4. **Video Processing** consome mensagem da fila
5. **Video Processing** processa vídeo (extrai frames)
6. **Video Processing** atualiza status via REST → **Upload API**
7. **Upload API** gera URL pré-assinada quando status = `COMPLETED`
8. **Cliente** consulta vídeos e baixa ZIP processado

## Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto faz parte do ecossistema FrameDrop.

## Autores

Desenvolvido por **Equipe Framedrop**

---

**Versão**: 0.0.1-SNAPSHOT  
**Última atualização**: Março 2026
