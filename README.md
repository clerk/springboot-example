### Spring boot reference app with Clerk API Authentication

This is a reference app that demonstrates how to use Clerk API to authenticate users in a Spring Boot application.

To run this application, follow the steps:

1. Ensure `CLERK_API_SECRET_KEY` environment variable is set with correct secret key.

2. Run following commands from terminal:
```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. From a Clerk frontend, use the useSession hook to retrieve the getToken() function:
```
const { getToken } = useSession();
```
4. Then send a request to backend server:
```
await fetch("http://localhost:8080/clerk_jwt", {
       headers: {
          "Authorization": `Bearer ${token}`
       }
   });
```

## Important Note:
This project is not optimized for production and do not address all best practices that should be configured in a production app (401 error handling and HTTPs for example).
These projects serve as a design template and should be given appropriate consideration before being used in production.