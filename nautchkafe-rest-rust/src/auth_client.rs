use reqwest::Error;
use crate::models::AuthUserProfile;
use serde_json::Value;
use futures::TryFutureExt;

pub struct AuthRestClient {
    client: reqwest::Client,
}

impl AuthRestClient {
    
    pub fn new() -> Self {
        AuthRestClient {
            client: reqwest::Client::new(),
        }
    }

    pub async fn get(&self, url: &str) -> Result<String, String> {
        self.client
            .get(url)
            .send()
            .await
            .map_err(|e| e.to_string())
            .and_then(|res| res.text().await.map_err(|e| e.to_string()))
    }

    pub fn parse_response(response: &str, provider: ApiProvider) -> Result<AuthUserProfile, String> {
        match provider {
            ApiProvider::Mojang => Self::parse_mojang_response(response),
            ApiProvider::Ashcon => Self::parse_ashcon_response(response),
        }
    }

    fn parse_mojang_response(response: &str) -> Result<AuthUserProfile, String> {
        let v: Value = serde_json::from_str(response).map_err(|e| e.to_string())?;
        let uuid = v["id"].as_str().ok_or("Missing id")?.to_string();
        let username = v["name"].as_str().ok_or("Missing name")?.to_string();

        Ok(AuthUserProfile {
            uuid: Uuid::parse_str(&uuid).map_err(|e| e.to_string())?,
            username,
            premium: false,
        })
    }

    fn parse_ashcon_response(response: &str) -> Result<AuthUserProfile, String> {
        let v: Value = serde_json::from_str(response).map_err(|e| e.to_string())?;
        let uuid = v["uuid"].as_str().ok_or("Missing uuid")?.to_string();
        let username = v["username"].as_str().ok_or("Missing username")?.to_string();

        Ok(AuthUserProfile {
            uuid: Uuid::parse_str(&uuid).map_err(|e| e.to_string())?,
            username,
            premium: false, 
        })
    }
}

#[derive(Debug, Clone, Copy)]
pub enum ApiProvider {
    Mojang,
    Ashcon,
}