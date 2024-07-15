# Social Network

---

Study project used to experiment on techniques that social networks (specially twitter) uses to make fast, reliable
and scalable systems.

## Usage

---
### For more details, run the command below to view the command line options
Run `make`

### Testcontainers
Testcontainers can have long loading times. To mitigate this, we can enable container reuse.

To enable container reuse, add the following line to the .testcontainers.properties file in your home directory:
```bash
testcontainers.reuse.enable=true
```
After running your tests, remember to manually stop the containers if needed.





