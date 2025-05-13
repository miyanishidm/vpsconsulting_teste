# Sistema de Gestão de Créditos para Parceiros - Desafio de Desenvolvimento Kotlin

## Autor

* Marcos Miyanishi Vargas Machado

## Contexto do Projeto

Este projeto foi desenvolvido como parte de um desafio técnico focado na criação de um microserviço para a gestão de créditos de parceiros em uma plataforma B2B. O sistema é projetado para ser robusto, escalável e capaz de lidar com milhares de requisições simultâneas, sendo um componente crítico para a operação de negócio ao gerenciar o ciclo de vida dos créditos: desde o reporte de vendas que podem gerar créditos, até o consumo desses créditos pelos parceiros.

## Descrição

O Sistema de Gestão de Créditos é um microserviço backend responsável por manter o saldo de créditos de cada parceiro, registrar transações (adição por vendas reportadas, subtração por consumo) e fornecer APIs para consulta e manipulação desses saldos. A principal preocupação é garantir a consistência e integridade dos dados em um ambiente de alta concorrência.


## Tecnologias Utilizadas (Sugestões, a serem definidas durante a implementação)

* **Linguagem:** Kotlin 
* **Framework:** Spring Boot
* **Banco de Dados:** PostgreSQL, (H2 para o teste)
* **Ferramenta de Build:** Maven

## Pré-requisitos

* Java Development Kit (JDK) 21
* Kotlin 2.1
* Maven 3.9.9
* Docker

## Como Configurar e Executar

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/miyanishidm/vpsconsulting_teste.git
    cd vpsconsulting_teste
    ```

2.  **Configure o Banco de Dados:**
    * Crie um banco de dados para o projeto.
    * Atualize as configurações de conexão no arquivo `application.properties` (geralmente em `src/main/resources`).

3.  **Build do Projeto:**
    * Usando Maven:
        ```bash
        mvn clean install
        ```
4.  **Executar a Aplicação:**
    * Usando Maven (Spring Boot):
        ```bash
        mvn spring-boot:run
        ```

## Testes

Para executar os testes:
* Usando Maven: `mvn test`


## Próximos Passos / Melhorias Futuras (Opcional)

* Implementação de autenticação e autorização para os endpoints da API.
* Configuração de um pipeline CI/CD.
