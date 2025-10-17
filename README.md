# Crypto Alert System

A comprehensive cryptocurrency Fear & Greed Index alert system built with Spring Boot and React.

## 🚀 Features

### Backend (Spring Boot)
- **Multi-module Architecture**: Clean separation of concerns with domain, service, infra, and api modules
- **JWT Authentication**: Secure user authentication with HttpOnly cookies
- **Fear & Greed Index Integration**: Real-time cryptocurrency market sentiment monitoring
- **Email Notifications**: Beautiful HTML email templates with Thymeleaf
- **SMS Notifications**: Integration with Solapi for SMS alerts
- **Alert Management**: Create, update, and manage alert conditions
- **Alert History**: Track all alert executions with detailed logs
- **Scheduled Alerts**: Automatic daily alert execution at 9 AM
- **Manual Alert Execution**: On-demand alert triggering

### Frontend (React)
- **Modern UI**: Clean and responsive design with consistent styling
- **Dashboard**: Real-time Fear & Greed Index display with historical charts
- **Alert Settings**: Easy configuration of alert conditions and channels
- **Active Alerts Management**: View, edit, and delete active alerts
- **Alert History**: Detailed history with message content popups
- **Account Management**: User profile management and settings
- **Responsive Design**: Optimized for desktop, tablet, and mobile devices

## 🏗️ Architecture

```
Crypto-Alert-multiModule/
├── api/                 # REST API controllers and configuration
├── common/              # Shared utilities and exceptions
├── domain/              # Entity models and repositories
├── infra/               # Infrastructure configuration
├── service/             # Business logic and services
└── frontend/            # React frontend application
```

## 🛠️ Tech Stack

### Backend
- **Java 17**
- **Spring Boot 2.7.12**
- **Spring Security** with JWT
- **Spring Data JPA** with Hibernate
- **H2 Database** (in-memory)
- **Thymeleaf** for email templates
- **JavaMail** for email sending
- **Solapi** for SMS notifications
- **Gradle** for build management

### Frontend
- **React 18**
- **Axios** for API communication
- **CSS3** with modern styling
- **Responsive Design**

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Node.js 16 or higher
- npm or yarn

### Backend Setup
1. Navigate to the project root
2. Run the backend:
```bash
./gradlew :api:bootRun
```

The backend will start on `http://localhost:8080`

### Frontend Setup
1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

The frontend will start on `http://localhost:3000`

## 📱 Features Overview

### Dashboard
- Real-time Fear & Greed Index display
- Historical index charts
- Current market status

### Alert Management
- Create custom alert conditions
- Set threshold values (0-100)
- Choose alert types (Above/Below)
- Select notification channels (Email/SMS)
- Edit and delete existing alerts

### Alert History
- View all executed alerts
- See detailed message content
- Track notification channels used
- Filter by date and type

### Account Settings
- Update user profile information
- Change password
- Manage contact information
- View account status

## 🔧 Configuration

### Email Configuration
Update `application.yml` with your email settings:
```yaml
mail:
  host: smtp.gmail.com
  port: 587
  username: your-email@gmail.com
  password: your-app-password
```

### SMS Configuration
Update `application.yml` with your Solapi settings:
```yaml
solapi:
  api-key: YOUR_SOLAPI_API_KEY
  api-secret: YOUR_SOLAPI_API_SECRET
  from: YOUR_SENDER_PHONE_NUMBER
```

## 📊 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `GET /api/auth/me` - Get current user info
- `PUT /api/auth/me` - Update user info

### Alerts
- `GET /api/alerts` - Get all alerts
- `POST /api/alerts` - Create new alert
- `PUT /api/alerts/{id}` - Update alert
- `DELETE /api/alerts/{id}` - Delete alert
- `POST /api/alert/send` - Execute alert manually

### Alert History
- `GET /api/alert-history` - Get alert history
- `GET /api/alert-history/{id}` - Get alert history detail
- `DELETE /api/alert-history/{id}` - Delete alert history

## 🎨 UI Components

### Main Components
- **Dashboard**: Main landing page with market overview
- **AlertSettingsCard**: Create new alert conditions
- **ActiveAlertList**: Manage existing alerts
- **AlertHistoryCard**: View alert execution history
- **AccountPage**: User profile management

### Design Features
- Consistent color scheme and typography
- Responsive grid layouts
- Interactive buttons and forms
- Modal dialogs for confirmations
- Loading states and error handling

## 🔒 Security

- JWT-based authentication
- HttpOnly cookies for token storage
- Password encryption with BCrypt
- CORS configuration for frontend integration
- Input validation and sanitization

## 📈 Future Enhancements

- [ ] Push notifications
- [ ] Multiple cryptocurrency support
- [ ] Advanced charting and analytics
- [ ] User roles and permissions
- [ ] Email template customization
- [ ] API rate limiting
- [ ] Database migration scripts
- [ ] Docker containerization

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author

**HongDaniel**
- GitHub: [@HongDaniel](https://github.com/HongDaniel)

## 🙏 Acknowledgments

- Fear & Greed Index data provided by [Alternative.me](https://alternative.me/crypto/fear-and-greed-index/)
- Email templates inspired by modern design principles
- React components built with accessibility in mind
