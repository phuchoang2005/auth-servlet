# Business Logic Requirements — Product Discovery

## 1. Overview
The **Product Discovery** module is designed to provide a seamless browsing experience. It serves as the bridge between the backend (Java Servlets) and the frontend (React/Angular/Ajax) by providing dynamic APIs to retrieve product data asynchronously.

### Key Objectives:
* **Decoupled Architecture:** Communication is strictly via **JSON**, mimicking a RESTful environment.
* **Asynchronous UX:** All data fetching is handled via **Ajax** to prevent page reloads.
* **Scalability:** Implementation of server-side pagination and optimized filtering.

---

## 2. API Standard Specifications

### 2.1 Success Response (`ApiResponse<T>`)
Every successful request must return a consistent JSON structure:
```json
{
  "status": "success",
  "timestamp": "2026-04-12T13:00:00Z",
  "message": "Operation completed successfully",
  "data": {
    "content": [
      {
        "productId": 0,
        "productName": "string",
        "unit": "string",
        "price": 0,
        "stock": 0,
        "imageUrl": "string",
        "categoryId": 0
      }
    ],
    "pagination": {
      "page": 0,
      "size": 0,
      "totalElements": 0,
      "totalPages": 0
    }
  }
}
```

### 2.2 Error Response (`ApiErrorResponse`)
In case of failures, the system must return:
```json
{
  "status": "error",
  "timestamp": "2026-04-12T09:41:12.321Z",
  "errorCode": "PRODUCT_NOT_FOUND",
  "message": "The requested product does not exist",
  "path": "/api/products/999"
}
```

---

## 3. Functional Requirements

### 3.1 Product Listing & Filtering
The system must provide an endpoint (e.g., `/api/products`) to fetch products with the following logic:
* **Category Filter:** If a `categoryId` is provided, return only products belonging to that category.
* **Sorting:** Support `sortBy` (price, name) and `order` (asc, desc) parameters. Default to `id DESC`.
* **Data Fields:** Each product object must include `id`, `name`, `unit`, `price`, `stock`, `imageUrl`, and `categoryId`.

### 3.2 Live Search
* **Partial Matching:** Search for products where the name contains the `keyword` (using SQL `LIKE keyword%` for indexing).
* **Case Sensitivity:** The search should be case-insensitive to improve user experience.
* **Empty State:** If no keyword is provided, the system defaults to returning the standard paginated list.

### 3.3 Server-Side Pagination
To maintain performance with large datasets, pagination must be calculated at the database level:
* **Input Params:** `page` (starting at 0), `size` (number of items).
* **Calculations:**
    * $$offset = page \times size$$
    * $$totalPages = \lceil \frac{totalElements}{size} \rceil$$
* **Implementation:** Use `LIMIT` and `OFFSET` in the SQL queries.

### 3.4 Product Quick View
* **Specific Lookup:** Retrieve full details of a single product using its `product_id`.
* **Inventory Status:** The system should calculate and return an `isAvailable` boolean based on `stock > 0`.

---

## 4. API Endpoint Mapping

| Method | Endpoint | Description | Query Parameters |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/products` | Fetch paginated products | `page, size, categoryId, keyword, sortBy` |
| `GET` | `/api/products/detail` | Get single product details | `id` |
| `GET` | `/api/categories` | Fetch all categories | N/A |

---

## 5. Technical Constraints & Security

### 5.1 Performance Requirements
1.  **Database Indexing:** Indexes must be applied to `product_name` and `category_id` to ensure $O(\log n)$ search complexity.
2.  **Resource Management:** Servlets must explicitly close `ResultSet`, `PreparedStatement`, and `Connection` (preferably using **try-with-resources**).

### 5.2 Security Considerations
1.  **SQL Injection:** Use `PreparedStatement` with placeholders (`?`) for all dynamic queries. **Never** concatenate strings into SQL.
2.  **Input Sanitization:** Validate that `page` and `size` are positive integers to prevent unexpected behavior.

---

### 5.3 Error Handling Mapping

| Scenario | HTTP Status | Error Code |
| :--- | :--- | :--- |
| Invalid input type (e.g., string for ID) | 400 Bad Request | `INVALID_INPUT` |
| Product ID does not exist | 404 Not Found | `PRODUCT_NOT_FOUND` |
| Internal SQL Exception | 500 Internal Error | `DATABASE_ERROR` |

---
