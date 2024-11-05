## Exercise 4 – Critical Analysis by comparing Standalone and Cloud Based Solution

Please use the below link to update your document.

- [CPU Memory Usage in mb](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EXNJlwFUaxlGobtxSnQC4isBxuBuxPyScnrjSyYkqXuy4A?e=Bnpd4z)
- [Available Nodes](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EWqRtr_TDLFGvkCQxJqmbu8B96XcW7DQqtYwd90j96DJkw?e=ur5EJi)
- [Execution Time in ms](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/ESF6WJXluwlCigCJniwcW7gBUNvSnMVxoBrPeinZZSO-Hw?e=cL40kZ)

## Execution time comparison in MS

The table below presents performance metrics comparing various operations on datasets of different sizes, allowing us to assess the two approaches—cloud-based and standalone. It provides insights into execution time (in milliseconds) and memory usage (in MB) for each operation, measured using Groovy and MongoDB, offering a detailed view of resource efficiency and processing times across data operations.

| Dataset              | JSON Size (MB) | MongoDB Size (MB) | Operation                 | Groovy Execution Time (ms) | MongoDB Execution Time (ms) | Groovy Memory Usage (MB) | MongoDB Memory Usage (MB) |
|----------------------|----------------|--------------------|----------------------------|----------------------------|-----------------------------|--------------------------|---------------------------|
| 2 months   | 10.9           | 2.11              | Selection All             | 232                        | 1324                        | 72.73                    | 27.75                     |
|                      |                |                    | Projection                | 205                        | 262                         | 38                       | 19                        |
|                      |                |                    | Filtering                 | 1762                       | 356                         | 20.55                    | 10                        |
|                      |                |                    | Data Combination   | 1780                         | 284                         | 22.31                     | 5.99                      |
|                      |                |                    |
| 4 months   | 22.4           | 5.01              | Selection All             | 295                        | 1877                        | 168.29                   | 100.75                    |
|                      |                |                    | Projection                | 290                        | 339                         | 25.56                    | 48                        |
|                      |                |                    | Filtering                 | 3508                       | 521                         | 5.52                     | 10                        |
|                      |                |                    | Data Combination    | 3527                         | 365                         | 8.53                     | 5                         |
|                      |                |                    |
| 2024 entire year      | 44.1           | 20.38             | Selection All             | 428                        | 2366                        | 403.38                   | 194.86                    |
|                      |                |                    | Selection 4m              | 534                        | 1552                        | 298.27                   | 56                        |
|                      |                |                    | Projection                | 364                        | 692                         | 66.02                    | 40                        |
|                      |                |                    | Filtering                 | 9358                       | 746                         | 842.98                   | 27.51                     |
|                      |                |                    | Data Combination    | 9385                         | 511                         | 847.03                     | 7                         |
|                      |                |                    |

## Available nodes


We used MongoDB Atlas to host our MongoDB database on the Free Tier M0, which provides a no-cost option for basic setups. The current configuration includes a primary node and two secondary nodes, enabling a basic replica set.

Our current configuration includes:

- **Write Mode**: *Majority*  
  Ensures that writes are acknowledged only after replication to a majority of the nodes, providing additional data consistency.

- **Read Mode**: *primaryPreferred*  
  Prioritises reading from the primary node but allows fallback to secondary nodes if the primary becomes unavailable.

To obtain the time and memory data for each query, we follow a structured approach to measure both the duration and memory usage accurately.

1. **Memory Usage**:
   - First, we determine the *used memory* by calculating the difference between the *total memory* available to the system and the *free memory* at a specific time.
   - Before executing a query, we record the current used memory. After the query completes, we record the used memory again.
   - By subtracting the initial memory value from the final value, we get the *exact amount of memory consumed* by that query.

2. **Execution Time**:
   - To measure execution time, we capture the *start time* just before the query runs and the *end time* immediately after it finishes.
   - The difference between the end time and start time gives the *total time taken* for the query to execute.
