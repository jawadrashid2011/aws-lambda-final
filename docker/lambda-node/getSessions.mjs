import KcAdminClient from "@keycloak/keycloak-admin-client";

const BASE_URL = process.env.BASE_URL || "http://keycloak_demo:8080";
const KEYCLOAK_GRANT_TYPE = process.env.KEYCLOAK_GRANT_TYPE || "client_credentials";
const KEYCLOAK_CLIENT_ID = process.env.KEYCLOAK_CLIENT_ID || "demo-client";
const KEYCLOAK_CLIENT_SECRET = process.env.KEYCLOAK_CLIENT_SECRET || "CLIENT_SECRET_GOES_HERE";
const KEYCLOAK_REALM = process.env.KEYCLOAK_REALM || "Demo";


export const handler = async (event) => {  
  const username = event.username || "";
  const realmName = event.realm || KEYCLOAK_REALM;

  /* Setting Environment and defaults */
  
  let output = [];

  const kcAdminClient = new KcAdminClient({
    baseUrl: BASE_URL,
  });
  
  await kcAdminClient.auth({
    grantType: KEYCLOAK_GRANT_TYPE,
    clientId: KEYCLOAK_CLIENT_ID,
    clientSecret: KEYCLOAK_CLIENT_SECRET,
  });
  
  // List first page of users
  kcAdminClient.setConfig({
    realmName: realmName,
  });
  
  const current_user = await kcAdminClient.users.find({
    username: username,
    exact: true,
  });
  // console.log("User", current_user);
  if (current_user.length == 0) {
    // console.log("No User Found");
    output = "No User Found";
  } else {
    const sessions = await kcAdminClient.users.listSessions({
      id: current_user[0].id,
    });
    // console.log("Sessions", sessions);
    output = sessions;
  }

  const response = {
    statusCode: 200,
    body: output,
  };
  return response;
};
