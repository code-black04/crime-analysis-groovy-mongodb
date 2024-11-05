## Exercise 5 – In depth Analysis and Implmentation of Pagination(chosen)

In this exercise the goal is to evaluate how different query types perform with and without pagination, focusing on execution time and memory usage.

Since our data is a stored dataset, the method that we use is offset based pagination which was implemented using MongoDB's skip and limit functions, this allows to retrieve smaller subsets of data (chunks or pages) to analyze the impact on resource usage and scalability.

Pagination is essential for handling large datasets efficiently, by dividing data into smaller pages we reduce the amount of data processed at once, leading to faster query response time and lower memory consumption.

As explained in exercise 1, 2 & 3 we implemented several types of queries, each of these queries were executed both with and without pagination, and we measured the execution time and memory usage for each case using the measureTimeAndExecution function from exercise 4.

## Impact of Pagination on Different Queries
1. Selection Query:
   - Without pagination: Retrieving all data took 619ms and 45 MB of memory, showing high resource demands.
   - With pagination: Both execution time and memory usage dropped significantly. For example, for page size of 10, execution time reduced to 169ms, and memory usage was minimal.

2. Projection Query:
   - Without pagination: Performing projection on full data took 664ms and 38 MB of memory.
   - With pagination: The performance was significantly improved with execution times as low as 14-16ms across different page sizes and almost zero memory usage.

3. Filtering Query:
   - Without pagination: This was the most resource hungry query, with execution time of 862ms and 20MB of memory usage.
   - With pagination: This query was still demanding with the resources having execution time of 530ms at page size of 10 and memory usage at 1-3 MB more usage.

4. Combination & Grouping Query:
   - Without pagination: This query took 577ms and ~6Mb of memory usage.
   - With pagination: While the execution time was slightly lower at 451ms but the memory usage was significantly lower at 1 MB usage.

## Conclusion
Using skip and limit for pagination effectively reduces resource consumption and also improves response times by limiting the amount of data that is retrieved. Pagination is a crucial strategy for managing large datasets, reducing the load on server and network. While skip and limit are effective for basic pagination, further optimizations of queries may be necessary to achieve optimal performance, especially for filtering, combination & grouping queries.

## Please find the Graphs depicting the conclusion

- [Average Time Execution in ms](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EcXsbQ8Mf2FKk3lRyp66Y98BOtDq01junBJGu8K7BPLsCA?e=9MoCc6)
- [Average Memory Usage in mb](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EQNXh_pf7JNBloiRsJoG8ysB_dxralZwQFSQ9SwHtq75tQ?e=Pr6H1k)
