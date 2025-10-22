pipeline {
    agent any
    
    environment {
        // 환경 변수 설정
        DOCKER_IMAGE = 'crypto-alert-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        EC2_HOST = 'your-ec2-ip'
        EC2_USER = 'ubuntu'
        EC2_KEY_PATH = '/var/lib/jenkins/.ssh/crypto-alert-key.pem'
        DEPLOY_PATH = '/home/ubuntu/crypto-alert-deploy'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📥 소스코드 체크아웃 중...'
                checkout scm
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
        
        stage('Docker Build') {
            steps {
                echo '🐳 Docker 이미지 빌드 중...'
                script {
                    // Dockerfile이 있는지 확인
                    if (fileExists('Dockerfile')) {
                        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                        sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                    } else {
                        echo '⚠️ Dockerfile이 없습니다. JAR 파일 직접 배포를 진행합니다.'
                    }
                }
            }
        }
        
        stage('Deploy to EC2') {
            steps {
                echo '🚀 EC2에 배포 중...'
                script {
                    // EC2에 배포 디렉토리 생성
                    sh """
                        ssh -i ${EC2_KEY_PATH} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} \
                        'mkdir -p ${DEPLOY_PATH}'
                    """
                    
                    // JAR 파일 복사
                    sh """
                        scp -i ${EC2_KEY_PATH} -o StrictHostKeyChecking=no \
                        api/build/libs/*.jar ${EC2_USER}@${EC2_HOST}:${DEPLOY_PATH}/
                    """
                    
                    // 설정 파일들 복사
                    sh """
                        scp -i ${EC2_KEY_PATH} -o StrictHostKeyChecking=no \
                        .env ${EC2_USER}@${EC2_HOST}:${DEPLOY_PATH}/
                    """
                    
                    // 배포 스크립트 실행
                    sh """
                        ssh -i ${EC2_KEY_PATH} -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} \
                        'cd ${DEPLOY_PATH} && chmod +x deploy.sh && ./deploy.sh'
                    """
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
