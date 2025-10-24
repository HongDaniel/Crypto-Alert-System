pipeline {
    agent any
    
    environment {
        // 환경 변수 설정
        AWS_REGION = 'ap-northeast-2'
        ECR_REGISTRY = '628856589662.dkr.ecr.ap-northeast-2.amazonaws.com/alert-system'
        DOCKER_IMAGE = 'crypto-alert-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        
        
        // EC2 설정 (하드코딩으로 임시 설정)
        EC2_HOST = '172.30.1.39'
        EC2_USER = 'ec2-user'
        DEPLOY_PATH = '/home/ec2-user/crypto-alert-deploy'
        
        // 환경변수 파일에서 읽어오기
        SPRING_PROFILES_ACTIVE = 'prod'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📥 소스코드 체크아웃 중...'
                checkout scm
            }
        }
        
        stage('Load Environment Variables') {
            steps {
                echo '🔧 환경변수 파일 로드 중...'
                script {
                    // 현재 작업 디렉토리 확인
                    echo "현재 작업 디렉토리: ${pwd()}"
                    
                    // .env 파일이 있는지 확인
                    if (fileExists('.env')) {
                        echo "✅ .env 파일 발견: ${pwd()}/.env"
                        // .env 파일 내용 출력
                        def envFile = readFile('.env')
                        echo "환경변수 파일 내용:"
                        echo envFile
                        
                        // EC2 설정을 환경변수로 설정
                        envFile.split('\n').each { line ->
                            if (line.trim() && !line.startsWith('#')) {
                                def parts = line.split('=', 2)
                                if (parts.length == 2) {
                                    def key = parts[0].trim()
                                    def value = parts[1].trim()
                                    
                                    // EC2 관련 설정만 환경변수로 설정
                                    if (key.startsWith('EC2_') || key.startsWith('DEPLOY_')) {
                                        env[key] = value
                                        echo "환경변수 설정: ${key} = ${value}"
                                    }
                                }
                            }
                        }
                        echo "✅ .env 파일 로드 완료"
                    } else {
                        echo "⚠️ .env 파일이 없습니다: ${pwd()}/.env"
                        echo "Jenkins 워크스페이스에 .env 파일을 복사해주세요."
                    }
                }
            }
        }
        
        stage('Build') {
            steps {
                echo '🔨 프로젝트 빌드 중...'
                sh '''
                    chmod +x gradlew
                    ./gradlew clean build
                '''
            }
        }
        
        stage('Test') {
            steps {
                echo '🧪 테스트 실행 중...'
                sh './gradlew test'
            }
        }
        
        stage('Docker Build & Push to ECR') {
            steps {
                echo '🐳 Docker 이미지 빌드 및 ECR 푸시 중...'
                script {
                    withCredentials([usernamePassword(credentialsId: 'aws-credentials', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        // AWS CLI 설치 확인
                        sh 'aws --version || echo "AWS CLI not found"'
                        
                        // ECR 로그인
                        sh """
                            aws ecr get-login-password --region ${AWS_REGION} | \
                            docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        """
                        
                        // Docker 이미지 빌드
                        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                        sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${ECR_REGISTRY}:${DOCKER_TAG}"
                        sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${ECR_REGISTRY}:latest"
                        
                        // ECR에 푸시
                        sh "docker push ${ECR_REGISTRY}:${DOCKER_TAG}"
                        sh "docker push ${ECR_REGISTRY}:latest"
                        
                        echo "✅ Docker 이미지가 ECR에 성공적으로 푸시되었습니다."
                    }
                }
            }
        }
        
        stage('Deploy to EC2') {
            steps {
                echo '🚀 EC2에 배포 중...'
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY')]) {
                        // EC2에서 기존 컨테이너 중지 및 제거
                        sh """
                            ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} \
                            'cd ${DEPLOY_PATH} && docker-compose down || true'
                        """
                        
                        // Jenkins에서 ECR 로그인 후 토큰을 EC2로 전송
                        sh """
                            ECR_TOKEN=$(aws ecr get-login-password --region ${AWS_REGION})
                            ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} \
                            "echo '${ECR_TOKEN}' | docker login --username AWS --password-stdin ${ECR_REGISTRY}"
                        """
                        
                        // EC2에서 최신 이미지 Pull
                        sh """
                            ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} \
                            'cd ${DEPLOY_PATH} && docker pull ${ECR_REGISTRY}:latest'
                        """
                        
                        // EC2에서 Docker Compose로 서비스 시작
                        sh """
                            ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} \
                            'cd ${DEPLOY_PATH} && docker-compose up -d'
                        """
                        
                        echo "✅ EC2 배포가 완료되었습니다."
                    }
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo '🏥 서비스 상태 확인 중...'
                script {
                    // 서비스가 정상적으로 시작되었는지 확인
                    sh """
                        sleep 30
                        curl -f http://${EC2_HOST}:8080/api/alert/test-sms || exit 1
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ 배포가 성공적으로 완료되었습니다!'
            // Slack 알림 (선택사항)
            // slackSend channel: '#deployments', 
            //          color: 'good', 
            //          message: "✅ Crypto Alert 앱이 성공적으로 배포되었습니다! (Build #${BUILD_NUMBER})"
        }
        failure {
            echo '❌ 배포가 실패했습니다.'
            // Slack 알림 (선택사항)
            // slackSend channel: '#deployments', 
            //          color: 'danger', 
            //          message: "❌ Crypto Alert 앱 배포가 실패했습니다! (Build #${BUILD_NUMBER})"
        }
        always {
            echo '🧹 빌드 정리 중...'
            // Docker 이미지 정리 (선택사항)
            sh 'docker system prune -f'
        }
    }
}
