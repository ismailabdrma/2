# stage2025v2main - E-commerce Application

This project is a full-stack e-commerce application built with Spring Boot (Java) for the backend and Angular (TypeScript) for the frontend. It includes features such as user authentication (login, signup, OTP verification, password reset), product management, shopping cart functionality, order processing, and administrative dashboards for managing users, products, categories, suppliers, and logs.

## Project Structure

The project is divided into two main parts:

*   \`backend\`: A Spring Boot application providing RESTful APIs.
*   \`frontend\`: An Angular application consuming the backend APIs.

## Backend Setup (Spring Boot)

1.  **Prerequisites**:
    *   Java Development Kit (JDK) 17 or higher
    *   Maven 3.6.x or higher
    *   MySQL Database (or another compatible database like PostgreSQL, H2 for development)

2.  **Database Configuration**:
    *   Open \`src/main/resources/application.properties\`.
    *   Configure your database connection details. For MySQL, it might look like this:
        \`\`\`properties
        spring.datasource.url=jdbc:mysql://localhost:3306/stage-db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
        spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
        spring.datasource.username=root
        # IMPORTANT: For production, ensure this password is NOT empty and is secure.
        spring.datasource.password=your_database_password
        \`\`\`
        Replace \`your_database_password\` with your actual database password.

3.  **JWT Secret Key**:
    *   In \`src/main/resources/application.properties\`, set a strong, randomly generated secret key for JWT token signing.
        \`\`\`properties
        # IMPORTANT: For production, this secret key should be a strong, randomly generated string
        # and ideally loaded from environment variables or a secure vault.
        application.security.jwt.secret-key=your_very_long_and_secure_jwt_secret_key_here_at_least_32_characters
        \`\`\`
        You can generate a strong key using a tool or a simple script (e.g., \`openssl rand -base64 32\`).

4.  **Stripe API Keys (Optional, for Payment)**:
    *   If you plan to use Stripe payments, set your Stripe secret key in \`application.properties\` or as an environment variable:
        \`\`\`properties
        stripe.api.key=${STRIPE_SECRET_KEY}
        stripe.webhook.secret=${STRIPE_SECRET_KEY} # Use the same key for webhook signing secret in development
        \`\`\`
    *   For production, it's recommended to use environment variables for these.

5.  **Email Service (Optional, for OTP/Password Reset)**:
    *   Configure your email credentials in \`application.properties\` if you want to enable OTP and password reset emails:
        \`\`\`properties
        spring.mail.host=smtp.gmail.com
        spring.mail.port=587
        spring.mail.username=your_email@gmail.com # Replace with your email
        spring.mail.password=your_app_password # Replace with your app password (for Gmail, generate one in Google Account Security)
        spring.mail.properties.mail.smtp.auth=true
        spring.mail.properties.mail.smtp.starttls.enable=true
        \`\`\`

6.  **Run the Backend**:
    *   Navigate to the root directory of the project (where \`pom.xml\` is located).
    *   Open a terminal or command prompt.
    *   Run the Spring Boot application using Maven:
        \`\`\`bash
        ./mvnw spring-boot:run
        \`\`\`
        (On Windows, use \`mvnw.cmd spring-boot:run\`)
    *   The backend will typically start on \`http://localhost:8080\`.

## Frontend Setup (Angular)

1.  **Prerequisites**:
    *   Node.js (LTS version recommended)
    *   npm (Node Package Manager) or Yarn
    *   Angular CLI (\`npm install -g @angular/cli\`)

2.  **Navigate to Frontend Directory**:
    *   Open a new terminal or command prompt.
    *   Navigate into the \`frontend\` directory:
        \`\`\`bash
        cd frontend
        \`\`\`

3.  **Install Dependencies**:
    *   Install the Angular project dependencies:
        \`\`\`bash
        npm install
        # or yarn install
        \`\`\`

4.  **Configure API URL**:
    *   Open \`frontend/src/environments/environment.ts\` and \`frontend/src/environments/environment.prod.ts\`.
    *   Ensure \`apiUrl\` points to your backend:
        \`\`\`typescript
        export const environment = {
          production: false,
          apiUrl: 'http://localhost:8080' // Or your backend URL
        };
        \`\`\`

5.  **Run the Frontend**:
    *   From the \`frontend\` directory, run the Angular development server:
        \`\`\`bash
        ng serve --open
        \`\`\`
    *   This will compile the Angular application and open it in your default web browser, usually at \`http://localhost:4200\`.

## Important Notes

*   **CORS**: The backend's \`SecurityConfig.java\` currently allows all origins (\`*\`) for CORS. For production deployments, it is highly recommended to restrict this to your specific frontend domain(s) for security reasons.
*   **Security**: Ensure all sensitive keys (JWT secret, database passwords, API keys) are properly secured and not hardcoded in production environments. Use environment variables or a secrets management service.
*   **Database**: For production, consider using a managed database service and a more robust database user with restricted permissions.
*   **Deployment**: For deployment, you would typically build the Angular application (\`ng build --configuration production\`) and serve it statically, or integrate it with the Spring Boot application as static resources. The Spring Boot application would then be packaged into a JAR and deployed to a server.
