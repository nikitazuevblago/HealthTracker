import psycopg2 as pg2
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
from typing import List, Optional
import os


load_dotenv()

# Database connection parameters
db_params = {
    "host": os.getenv("host"),
    "database": os.getenv("database"),
    "user": os.getenv("user"),
    "password": os.getenv("password")  # Replace with your actual password
}

app = FastAPI()

# CORS configuration
origins = [
    "http://localhost",
    "http://localhost:8080",
    "http://localhost:3000",  # React default port
    "https://yourdomain.com",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Create a user
@app.post('/createUSERS')
def createUSERS(username: str, email: str, password: str):
    try:
        conn = pg2.connect(**db_params)
    
        # Create a cursor object
        cur = conn.cursor()

        cur.execute("""SELECT * FROM USERS""")
        usersPostgres = cur.fetchall()
        userParams = {"email": [], "username": []}

        for idP, emailP, usernameP, passwordP in usersPostgres:
            userParams["email"].append(emailP)
            userParams["username"].append(usernameP)

        if email in userParams["email"]:
            return {"message" : "This email already exists!"}
        
        if username in userParams["username"]:
            return {"message" : "This username already exists!"}
        
        # Execute a sample query
        cur.execute("""
        INSERT INTO users (username, email, password) VALUES (%s, %s, %s);""",
        (username, email, password))

        conn.commit()
        result = f"User {username} added"
    except Exception as e:
        result = f"Couldn't add the user because of '{e}'"
    finally:
        # Close the cursor and connection
        if cur:
            cur.close()
        if conn:
            conn.close()

    return {"message" : f"{result}"}


# Find a user by some search parameter
@app.get('/readUSERS')
def readUSERS(id: Optional[int] = None, username: Optional[str] = None,
              email: Optional[str] = None):
    
    search_params = set([id, username, email])

    if len(search_params) == 1 and None in search_params:
        return {"message" : "No search param is provided!"}
    elif (len(search_params) == 2 and None not in search_params) or len(search_params)==3:
        return {"message" : "You entered more than 1 search param!"}
    else:
        if id!=None:
            selectedSearchParamName = "id"
            selectedSearchParam = id
            query = f"""SELECT * FROM USERS
                        WHERE {selectedSearchParamName} = {selectedSearchParam};"""
        elif username!=None:
            selectedSearchParamName = "username"
            selectedSearchParam = username
            query = f"""SELECT * FROM USERS
                        WHERE {selectedSearchParamName} = '{selectedSearchParam}';"""
        elif email!=None:
            selectedSearchParamName = "email"
            selectedSearchParam = email
            query = f"""SELECT * FROM USERS
                        WHERE {selectedSearchParamName} = '{selectedSearchParam}';"""
            
        try:
            conn = pg2.connect(**db_params)
        
            # Create a cursor object
            cur = conn.cursor()
            
            # Execute a sample query
            cur.execute(query)
            result = cur.fetchall()
            if result!=[]:
                return {"message" : f"Retrieved successfully!",
                    "data": {"id":result[0][0], "username":result[0][1],
                            "email":result[0][2], "password":result[0][3]}}
            else:
                return {"message" : f"No user found", "data" : None}
        except Exception as e:
            result = f"Couldn't retrieve users because of {e}"
            return {"message" : result}
        finally:
            # Close the cursor and connection
            if cur:
                cur.close()
            if conn:
                conn.close()
    

# Update a user by id
@app.put('/updateUSERS')
def updateUSERS(id: int, email: str, password: str, username: str):
    try:
        conn = pg2.connect(**db_params)
    
        # Create a cursor object
        cur = conn.cursor()
        
        # Execute a sample query
        query = f"""UPDATE USERS
                SET email = '{email}', password = '{password}', username = '{username}'
                WHERE id = {id}"""
        cur.execute(query)
        conn.commit()
        result = f"The user {username} updated"
        return {"message" : result}
    except Exception as e:
        result = f"Couldn't update USERS because of {e}"
        return {"message" : result}
    finally:
        # Close the cursor and connection
        if cur:
            cur.close()
        if conn:
            conn.close()


# Update a user by id
@app.delete('/deleteUSERS')
def delete(id: int):
    try:
        conn = pg2.connect(**db_params)
    
        # Create a cursor object
        cur = conn.cursor()
        
        # Execute a sample query
        query = f"""DELETE FROM USERS WHERE id = {id}"""
        cur.execute(query)
        conn.commit()
        result = f"The user with id {id} deleted"
        return {"message" : result}
    except Exception as e:
        result = f"Couldn't delete from USERS because of {e}"
        return {"message" : result}
    finally:
        # Close the cursor and connection
        if cur:
            cur.close()
        if conn:
            conn.close()