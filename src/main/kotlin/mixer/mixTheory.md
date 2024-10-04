**Introduction to Mix Theory**
An example of a mix with 3 tracks, each with multiple effects, that will be remixed into one variable:
```mermaid
graph TD
    subgraph MultiTrack
        input([Input])
        remix([Remix Instruction])
        gen([Generate])

        subgraph "Track 0"
            subgraph "Effect 0"
                a00([Instruction])
                g00([Generate])
            end
            subgraph "Effect 1"
                a01([Instruction])
                g01([Generate])
            end
            subgraph "Effect 2"
                a02([Instruction])
                g02([Generate])
            end
        end

        subgraph "Track 1"
            subgraph "Effect 0"
                a10([Instruction])
                g10([Generate])
            end
            subgraph "Effect 1 * 4"
                a11([Instruction])
                g11([Generate])
            end
            subgraph "Effect 2"
                a12([Instruction])
                g12([Generate])
            end
        end
        
        subgraph "Track 2"
            subgraph "Effect 0"
                a20([Instruction])
                g20([Generate])
            end
            subgraph "Effect 1"
                a21([Instruction])
                g21([Generate])
            end
            subgraph "Effect 2"
                a22([Instruction])
                g22([Generate])
            end
        end

        input --> a00 --> g00 --> a01 --> g01 --> a02 --> g02
        input --> a10 --> g10 --> a11 --> g11 --> a12 --> g12
        input --> a20 --> g20 --> a21 --> g21 --> a22 --> g22
        g02 & g12 & g22 --> remix --> gen
    end
```