Notula
=

Setup
==

The first steps have already been done, and you can skip to Adding the authority to the browser, and using it in nginx.
If you ever need to do the entire setup yourself, this are the steps:

Create local Certificate Authority:
```
openssl genrsa -out localCA.key 4096
openssl req -x509 -new -nodes -key localCA.key \
  -sha256 -days 3650 \
  -subj "/CN=Local Dev CA" \
  -out localCA.pem
```

Trust the Certificate Authority:
```
sudo cp localCA.pem /usr/local/share/ca-certificates/local-dev-ca.crt
sudo update-ca-certificates
```

Generate keys:
```
openssl genrsa -out localhost.key 2048

openssl req -new -key localhost.key \
  -out localhost.csr \
  -config localhost.cnf

openssl x509 -req \
  -in localhost.csr \
  -CA localCA.pem \
  -CAkey localCA.key \
  -CAcreateserial \
  -out localhost.crt \
  -days 825 \
  -sha256 \
  -extensions req_ext \
  -extfile localhost.cnf
```

Add authority to browser (Firefox):
```
Settings → Privacy & Security
Certificates → View Certificates
Authorities → Import → localCA.pem
```

Using in nginx:
```
docker run --name notula-nginx \
	-v ./nginx.conf:/etc/nginx/nginx.conf:ro \
	-v ./ssl:/etc/nginx/certs:ro \
	-p 4443:443 \
	-d nginx
```

Note: currently you still may have to update the ip addres the host.docker.internal binds to.
Note: on Linux, add the host mapping: `--add-host=host.docker.internal:host-gateway`
