**Introduction to Mix Theory**

Mix Theory is a conceptual framework that defines how values, represented as samples, can be manipulated, combined, and transformed within a type-safe context. This theory is inspired by the principles outlined in the GraphIQ paper. The theory revolves around the idea that all values exist within a space, and these values can be sampled, transformed, and mixed according to a set of rules.

**Core Concepts:**
1. **Space**: A container that holds a value within a specific context.
2. **Sampler**: A function or process that extracts or manipulates values from a space.
3. **Transformation**: A function that takes a value from one space and produces a new value, possibly in the same or a different space.
4. **Mixing**: The process of combining multiple values from various spaces into a single output.

**Rules of Mix Theory:**
1. All values exist in a space and at a specific time.
2. Values can be sampled using samplers, which extract or manipulate these values.
3. The timing of when a value is sampled is crucial, as values may change over time.
4. Transformations apply at specific events in time, altering the values within a space.
5. Each value exists within a particular type of space, which dictates how it can be manipulated.
6. Values in the same space can be grouped together.
7. Grouped values can be mixed into a single space, creating a unified output.
8. Samples can be time-bound, known as `SampleTime`.
9. Sample times are organized within a `TimeGraph`.
10. Disjoint samplers can be run together, allowing for parallel processing.
11. Cross-functions allow changing type spaces for functions.
12. Sampling can be constrained using limiters.
