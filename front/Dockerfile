FROM node:18-alpine
WORKDIR /app
COPY package.json .
COPY package-lock.json .
RUN npm config set legacy-peer-deps true
RUN npm install
COPY . .
EXPOSE 5173
CMD [ "npm", "run", "dev" ]
