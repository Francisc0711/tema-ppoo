# Java OOP Standards

## OOP-1. Strict Encapsulation
All class fields must be `private` and accessed only via getters and setters.

## OOP-2. Inheritance and Polymorphism
Use abstract classes or interfaces where multiple classes share common behavior or signatures.

## OOP-3. Resource Management
Always use `try-with-resources` blocks when reading from or writing to files.

## OOP-4. Data Persistence Constraint
Data must be saved and loaded strictly using Java File I/O (text or binary files). The use of any SQL or NoSQL database connection is strictly forbidden. Use collections (ArrayList, HashMap) to manage data in memory.
