#!/bin/sh
host="$1"
port="$2"
shift 2
cmd="$@"

until nc -z "$host" "$port"; do
  echo "⏳ Esperando a que $host:$port esté listo..."
  sleep 2
done

echo "✅ $host:$port está listo. Ejecutando la app..."
exec $cmd
