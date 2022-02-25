package com.apiSimulatorPix.resource;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.apiSimulatorPix.service.BancoService;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

import io.quarkus.oidc.IdToken;
import io.quarkus.oidc.RefreshToken;
import io.quarkus.security.Authenticated;

import org.eclipse.microprofile.openapi.annotations.media.Content;

@Path("/banco")
@Tags(value = @Tag(name="Banco", description = "Configuração geral do banco"))
@Authenticated
public class BancoResource {
    @Inject
    @IdToken
    JsonWebToken idToken;

    @Inject
    JsonWebToken accessToken;

    @Inject
    RefreshToken refreshToken;

    @GET
    @Path("/ObterMeusDados")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation( summary = "Obter os meus dados cadastrados",
                description = "Obtem o nome, dinheiro e email registrados em sua conta.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Obtem os meus dados.", content =
                        @Content( mediaType = MediaType.TEXT_PLAIN)),
        @APIResponse(responseCode = "401", description = "Seu token de acesso está incorreto ou não foi informado."),
        @APIResponse(responseCode = "403", description = "Você não tem permissão para efetuar essa operação"),
        @APIResponse(responseCode = "500", description = "Algum problema foi detectado.")
    })
    public String getMyData()
    {
        List<BancoService> user = BancoService.listAll();
        for (int i = 0; i < user.size(); i++)
            if(accessToken.getName().equals(user.get(i).getName())) {
                return "Nome: " + user.get(i).getName() + 
                        "\nDinheiro: " + user.get(i).getMoney() + 
                        "\nEmail: " + user.get(i).getEmail();
            }
        return "Problemas ao recuperar seus dados.";
    }

    @GET
    @Path("/ObterDadosCadastrados")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation( summary = "Obter os dados cadastrados dos clientes",
                description = "Obtem o nome, dinheiro e email registrados através da ID.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Obtem os dados do usuário.", content =
                        @Content( mediaType = MediaType.TEXT_PLAIN)),
        @APIResponse(responseCode = "401", description = "Seu token de acesso está incorreto ou não foi informado."),
        @APIResponse(responseCode = "403", description = "Você não tem permissão para efetuar essa operação"),
        @APIResponse(responseCode = "500", description = "Algum problema foi detectado.")
    })
    public String getNameAndMoneyId(@QueryParam("id") Integer id)
    {
        BancoService user = ((BancoService)BancoService.listAll().get(id));
        return "Nome: " + user.getName() + "\nDinheiro: " + user.getMoney();
    }

    @POST
    @Path("/RegistrarConta")
    @Operation( summary = "Registrar uma nova conta",
                description = "Informe seu email para criar uma nova conta.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Cria uma conta com sucesso.", content =
                        @Content( mediaType = MediaType.TEXT_PLAIN)),
        @APIResponse(responseCode = "401", description = "Seu token de acesso está incorreto ou não foi informado."),
        @APIResponse(responseCode = "403", description = "Você não tem permissão para efetuar essa operação"),
        @APIResponse(responseCode = "500", description = "Algum problema foi detectado.")
    })
    @Transactional
    public void createAccount(@QueryParam("email") String email)
    {
        BancoService user = new BancoService();
        user.setName(accessToken.getName());
        user.setMoney(0.f);
        user.setEmail(email);
        BancoService.persist(user);
    }

    @POST
    @Path("/Pix")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @Transactional
    @Operation(summary = "Envia um pix")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Envia o pix com sucesso.", content =
                     @Content( mediaType = MediaType.TEXT_PLAIN)),
        @APIResponse(responseCode = "401", description = "Seu token de acesso está incorreto ou não foi informado."),
        @APIResponse(responseCode = "403", description = "Você não tem permissão para efetuar essa operação"),
        @APIResponse(responseCode = "500", description = "Algum problema foi detectado.")
    })
    public String sendPix(  @QueryParam("userReceive") String userReceive,
                            @QueryParam("value") Long value)
    {
        List<BancoService> user = BancoService.listAll();
        int id_Sent = -1, id_Receive = -1;

        for (int i = 0; i < user.size(); i++) {
            if(userReceive.equals(user.get(i).getName())) id_Receive = i;
            if(i == user.size()-1) return "Usuário não encontrado.";
        }

        for (int i = 0; i < user.size(); i++)
            if(accessToken.getName().equals(user.get(i).getName()))
                id_Sent = i;
        
        if(user.get(id_Sent).getMoney() >= value) {
            user.get(id_Sent).setMoney(user.get(id_Sent).getMoney() - value);
            user.get(id_Receive).setMoney(user.get(id_Receive).getMoney() + value);
            user.get(id_Sent).persist();
            user.get(id_Receive).persist();
            return "Pix de R$" + value + " enviado com sucesso!";
        }
        else
            return "Você não tem o valor necessário para essa transação!";
    }
}