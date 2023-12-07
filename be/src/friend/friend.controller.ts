import { Body, Controller, Delete, Get, Logger, Post, UseGuards } from "@nestjs/common";
import { FriendService } from "./friend.service";
import { AddFriendDto } from "./dto/add-friend.dto";
import { AuthGuard } from "src/guard/auth.guard";
import { Token } from "src/utils/token.decorator";
import { ApiOperation, ApiTags } from "@nestjs/swagger";

@ApiTags("친구")
@Controller("/api/friend")
@UseGuards(AuthGuard)
export class FriendController {
  private readonly logger = new Logger(FriendController.name);
  constructor(private friendService: FriendService) {}

  @ApiOperation({ summary: "친구 추가" })
  @Post("/add")
  async add(@Token() token: string, @Body() dto: AddFriendDto): Promise<JSON> {
    this.logger.log("Post /api/friend/add");
    this.logger.verbose("Data: " + JSON.stringify(dto, null, 2));
    const result = await this.friendService.add(token, dto);
    return JSON.parse(result);
  }

  @ApiOperation({ summary: "친구 목록 조회" })
  @Get("/")
  async getAllFriends(@Token() token: string) {
    this.logger.log("Get /api/friend/");
    this.logger.verbose("Token: " + token);
    const result = await this.friendService.getAllFriends(token);
    return JSON.parse(result);
  }

  @ApiOperation({ summary: "친구 삭제" })
  @Delete()
  async deleteFriend(@Token() token: string, @Body("email") email: string): Promise<JSON> {
    this.logger.log("Delete /api/friend/");
    this.logger.verbose("Token: " + token);
    this.logger.verbose("Email: " + email);
    const result = await this.friendService.deleteFriend(token, email);
    return JSON.parse(result);
  }
}
