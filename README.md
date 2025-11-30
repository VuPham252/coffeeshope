For customer app:

Use Cases: 
- Register, Login, Logout
- Find nearest shop, find all shops, order, payment, cancel order, track queue position, get shop menu

Standards:
- RESTful API design
- JSON for data interchange
- Naming conventions: camelCase for variables and functions, PascalCase for classes
- Security: HTTPS, JWT for authentication, input validation
- Error handling: Standard HTTP status codes, custom error messages in JSON format

Security solution:
- Authentication: JWT tokens for secure user sessions with Spring Security
- Authorization: Role-based access control (RBAC) to restrict access to certain endpoints

Testing:
- Unit tests: JUnit and Mockito for service layer testing
- Integration tests: Spring Boot Test for end-to-end testing of API endpoints
- API testing: Postman

APIs and business flows:
Domain: Coffeeshop-api-env.eba-7pfphpwk.ap-southeast-1.elasticbeanstalk.com
- Customer register:  POST /api/customer/auth/register
- Customer login:  POST /api/customer/auth/login
- Find nearest shop: GET /api/shops/nearest?latitude={latitude}&longitude={longitude}
- Get shop menu: GET  /api/shops/{shopId}/menu
- Place order: POST /api/orders
- Track queue position: GET  /api/orders/{orderId}/queue

Extra APIS:
- Customer logout: POST /api/customer/auth/logout
- Find all shops: GET /api/shops
- Cancel order: POST /api/orders/{orderId}/cancel
- Get all orders: GET /api//orders
- Get order in shop: GET /api/orders/shop/{shopId}